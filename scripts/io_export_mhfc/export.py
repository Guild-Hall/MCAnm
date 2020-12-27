from collections import defaultdict
from contextlib import ExitStack
import bmesh
import bpy
import re
import struct

from .utils import Reporter, openw_save, extract_safe


class Writer(object):
    _path = None
    _file = None

    def __init__(self, filepath):
        self._path = filepath

    def __enter__(self):
        self._file = openw_save(self._path, 'wb')
        return self

    def __exit__(self, *args):
        return self._file.__exit__(*args)

    def write_bytes(self, bytestring):
        self._file.write(bytestring)

    def write_string(self, string):
        """Writes a String to a file
        """
        data = string.encode("utf-8") + b'\x00'
        self.write_bytes(data)

    def write_packed(self, fmt, *args):
        """Packs the given data into the given bytebuffer using the given format
        """
        data = struct.pack(fmt, *args)
        self.write_bytes(data)


class ByteIndex(object):

    def __init__(self, idx):
        self.idx = idx

    def __lt__(self, other):
        return self.idx < other.idx

    def __eq__(self, other):
        return self.idx == other.idx

    def dump(self, writer):
        writer.write_packed(">B", self.idx)


class Bone(object):

    def __init__(self, bone):
        """
        bone Bone: can't be None, the bone this is initialized from
        """
        if bone is None:
            Reporter.fatal("(Bone) bone is None")
        self.bone = bone
        self.name = bone.name
        matrix_parent = bone.matrix_local
        if bone.parent is not None:
            matrix_parent = bone.parent.matrix_local.inverted() @ matrix_parent
        self.quat = matrix_parent.to_quaternion()
        self.translate = matrix_parent.translation

    def dump(self, writer):
        q = self.quat
        t = self.translate
        writer.write_string(self.name)
        writer.write_packed(">7f", q.x, q.y, q.z, q.w,
                            t.x, t.y, t.z)


class Point(object):

    def __init__(self, loop, uv_layer, deform_layer, arm_vgroup_idxs):
        """
        loop Loop: Can't be none, the loop this points represents
        uv_layer LLayerItem: Can't be None, the uv_layer we export
        deform_layer VLayerItem: Can be None, indicates that no bindings
                                                         should be exported
        arm_vgroup_idxs list: Can't be None, value at index i represents the index of the
                                                  arm.bones[i]'s vertex group in object
        """
        if loop is None:
            Reporter.fatal("(Point) Loop is None")
        vtx = loop.vert
        self.coords = vtx.co
        self.normal = vtx.normal
        self.uv = loop[uv_layer].uv
        self.bindings = []
        # calculate bindings
        if deform_layer is None:
            return

        def insert_into_binds(bind):
            """
            bind tuple: Can't be None, (idx, value) of binding
            """
            if bind is None:
                Reporter.fatal("(Point) Inserted tupple is None")
            self.bindings.append(bind)
        # iterate over all bones (as indices)
        for idx, vgroup_idx in enumerate(arm_vgroup_idxs):
            if vgroup_idx == -1:
                # bone has no vertex group
                continue
            try:
                value = vtx[deform_layer][vgroup_idx]
            except KeyError:
                # no binding to this bone
                pass
            else:
                if value > 0:
                    insert_into_binds((idx, value))

    def dump(self, writer):
        binds = sorted(self.bindings, key=lambda x: x[1], reverse=True)[:4]
        co = self.coords
        norm = self.normal
        uv = self.uv
        writer.write_packed(
            ">8f" + "Bf" * len(binds),
            co.x, co.y, co.z,
            norm.x, norm.y, norm.z,
            uv.x, 1 - uv.y,
            *[v for bind in binds for v in bind])
        if len(binds) < 4:
            writer.write_bytes(b'\xFF')

    def __eq__(self, other):
        epsilon = 0.001
        return (self.bindings == other.bindings and
                (self.uv - other.uv).length < epsilon and
                (self.coords - other.coords).length < epsilon and
                (self.normal - other.normal).length < epsilon)

    def __str__(self):
        return "Point(co={co}, normal={norm}, uv={uv}, bindings={binds})".format(
            co=self.coords,
            norm=self.normal,
            uv=self.uv,
            binds=self.bindings)


def guess_image_location(material):
    if not material.use_nodes:
        return None
    texture = next((t for t in material.node_tree.nodes if t.type == 'TEX_IMAGE'), None)
    return texture.image.name


class MaterialV1(object):
    def __init__(self, options, material):
        self.material = material
        self.img_location = guess_image_location(material)

    def dump(self, writer):
        if not self.img_location:
            Reporter.error("Couldn't determine image to use for {mat}", mat=self.material)
        written = bpy.path.ensure_ext(self.img_location, '.png')[:-4]
        writer.write_string(written)


class MaterialV2(object):
    def __init__(self, options, material):
        self.material = material
        self.material_name = material.name
    
    def __hash__(self):
        return hash(self.material_name)
    
    def __eq__(self, other):
        return self.material == other.material
    
    def dump(self, writer):
        writer.write_string(self.material_name)


class Part(object):

    def __init__(self, material, options, material_resolve):
        """
        material Material: can be None
                in case of None, it is a default material
        """
        self.name = material.name if material else "default"
        self.material_ref = material_resolve(material)
        # maps vertices to (point, list-index) pair-list
        self.point_map = defaultdict(list)
        # a full list of all points
        self.points = []
        self.indices = []

    def append_tri(self, tri_loops, uv_layer, deform_layer, arm_vgroup_idxs):
        """
        uv_layer LLayerItem: Can't be None, the uv_layer we export
        deform_layer VLayerItem: Can be None, indicates that no bindings
                                                         should be exported
        arm_vgroup_idxs list: Can't be None, value at index i represents the index of the
                                                  arm.bones[i]'s vertex group in object
        """
        def key_from_loop(l):
            return l.vert.index
        for loop in tri_loops:
            key = key_from_loop(loop)
            # append points or reuse existant one's
            new_point = Point(loop, uv_layer, deform_layer, arm_vgroup_idxs)
            index = -1
            known_points = self.point_map[key]
            for point, idx in known_points:
                if point == new_point:
                    index = idx
                    break
            else:
                index = len(self.points)
                self.points.append(new_point)
                known_points.append((new_point, index))
            assert index >= 0
            self.indices.append(index)

    def dump(self, writer):
        points = self.points
        p_len = len(self.points)
        idxs = self.indices
        idxs_len = len(idxs)
        if idxs_len % 3:
            Reporter.fatal("(Part) number of indices not divisible by 3")
        tris = idxs_len // 3
        if tris >= 2**16:
            Reporter.error(
                "(Part) too many tris in part {name}", name=self.name)
        writer.write_packed(">2H", p_len, tris)
        writer.write_string(self.name)
        self.material_ref.dump(writer)
        for point in points:
            point.dump(writer)
        writer.write_packed(">{idxs}H".format(idxs=idxs_len), *idxs)


class Animation(object):

    def __init__(self, fcurve, start):
        def write_all(*write_funcs):
            def wrapped(writer):
                for fn in write_funcs:
                    fn(writer)
            return wrapped

        def write_point(co):
            def wrapped(writer):
                writer.write_packed(">2f", co[0] - self.offset, co[1])
            return wrapped

        def constant_between(left, right):
            def wrapped(writer):
                writer.write_packed(">B", 8)
            return write_all(write_point(right.co), wrapped)

        def linear_between(left, right):
            def wrapped(writer):
                writer.write_packed(">B", 9)
            return write_all(write_point(right.co), wrapped)

        def bezier_between(left, right):
            def wrapped(writer):
                write_point(right.co)
                writer.write_packed(">B", 10)
            return write_all(write_point(right.co),
                             wrapped,
                             write_point(left.handle_left),
                             write_point(right.handle_right))

        def constant_extrapolation(writer):
            writer.write_packed(">B", 16)

        def linear_extrapolation(writer):
            writer.write_packed(">B", 17)
            write_point(points[-1].handle_right)(writer)

        interpolations = {
            'CONSTANT': constant_between,
            'LINEAR': linear_between,
            'BEZIER': bezier_between}
        extrapolations = {
            'CONSTANT': constant_extrapolation,
            'LINEAR': linear_extrapolation}

        self.offset = start
        if fcurve is None:
            self.write_all = lambda writer: writer.write_packed(">H", 0)
        else:
            points = [keyf for keyf in fcurve.keyframe_points]
            extrapolation_mode = fcurve.extrapolation

            anim_len = len(points)
            if anim_len > 2**16 - 1:
                Reporter.error("Too many keyframes in fcurve to export")

            def write_tunein(writer):
                writer.write_packed(">H", anim_len)
                if anim_len > 0:
                    write_point(points[0].co)(writer)
                    writer.write_packed(">B", 0)
            write_seq = write_all(
                *[extract_safe(
                    interpolations, right.interpolation,
                    "Unknown interpolation mode {item}")(left, right)
                  for left, right in zip(points[:], points[1:])]
            )
            write_end = extract_safe(
                extrapolations, extrapolation_mode,
                "Unknown extrapolation mode {item}")
            self.write_all = write_all(write_tunein, write_seq, write_end)

    def dump(self, writer):
        self.write_all(writer)


class BoneAction(object):

    def __init__(self, name, curves, offset):
        loc_curves, rot_curves, scale_curves = curves
        self.name = name
        self.loc_x = Animation(loc_curves[0], offset)
        self.loc_y = Animation(loc_curves[1], offset)
        self.loc_z = Animation(loc_curves[2], offset)

        self.rot_w = Animation(rot_curves[0], offset)
        self.rot_x = Animation(rot_curves[1], offset)
        self.rot_y = Animation(rot_curves[2], offset)
        self.rot_z = Animation(rot_curves[3], offset)

        self.scale_x = Animation(scale_curves[0], offset)
        self.scale_y = Animation(scale_curves[1], offset)
        self.scale_z = Animation(scale_curves[2], offset)

    def dump(self, writer):
        writer.write_string(self.name)
        self.loc_x.dump(writer)
        self.loc_y.dump(writer)
        self.loc_z.dump(writer)

        self.rot_x.dump(writer)
        self.rot_y.dump(writer)
        self.rot_z.dump(writer)
        self.rot_w.dump(writer)

        self.scale_x.dump(writer)
        self.scale_y.dump(writer)
        self.scale_z.dump(writer)


class MeshAbstract(object):

    def __init__(self, options, bm, resolve_mat):
        obj = options.obj
        mesh = obj.data
        # the armature to that object
        arm = options.arm
        # for each material we make one part
        self.part_dict = {}
        # checked
        uv_layer = bm.loops.layers.uv[options.uv_layer.name]
        # could be None
        deform_layer = bm.verts.layers.deform.active
        # sorted bones
        self.sorted_bones = sort_bones(arm)
        sorted_bones = self.sorted_bones
        # never none
        arm_vgroup_idxs = [] if sorted_bones is None else [
            obj.vertex_groups.find(bone.name) for bone in sorted_bones]
        for abc in bm.calc_loop_triangles():
            g_idx = abc[0].face.material_index
            material = mesh.materials[g_idx] if mesh.materials else None
            if g_idx not in self.part_dict:
                self.part_dict[g_idx] = Part(material, options, resolve_mat)
            self.part_dict[g_idx].append_tri(
                abc, uv_layer, deform_layer, arm_vgroup_idxs)
        if len(self.part_dict) > 0xFF:
            Reporter.error("Too many parts")


class MeshV1(MeshAbstract):

    def __init__(self, options, bmesh):
        super().__init__(options, bmesh, lambda g: MaterialV1(options, g))
        sorted_bones = self.sorted_bones
        self.bones = ([] if sorted_bones is None else
                      [Bone(bone) for bone in sorted_bones])
        if len(self.bones) > 0xFF:
            Reporter.error("Too many bones")
        self.bone_parents = ([] if sorted_bones is None else
                             [sorted_bones.find(b.parent.name) & 0xFF
                              if b.parent is not None
                              else 0xFF
                              for b in sorted_bones])

    def dump(self, writer):
        part_dict = self.part_dict
        bones = self.bones
        bone_parents = self.bone_parents
        writer.write_packed(">I", 1)
        writer.write_packed(">2B", len(part_dict), len(bones))
        for part in part_dict.values():
            part.dump(writer)
        for bone in bones:
            bone.dump(writer)
        writer.write_packed(">{nums}B".format(nums=len(bones)), *bone_parents)
        summary = 'Exported with ({numparts} parts, {numbones} bones)'.format(
            numparts=len(part_dict),
            numbones=len(bones)
        )
        Reporter.info(summary)


class MeshV2(MeshAbstract):

    def __init__(self, options, bmesh):
        self.mat_dict = defaultdict(lambda: ByteIndex(len(self.mat_dict)))
        super().__init__(
            options, bmesh, lambda g: self.mat_dict[MaterialV2(options, g)])

    def dump(self, writer):
        part_dict = self.part_dict
        mat_dict = self.mat_dict
        writer.write_packed(">I", 2)
        writer.write_packed(">2B", len(part_dict), len(mat_dict))
        for part in part_dict.values():
            part.dump(writer)
        for mat, _ in sorted(mat_dict.items(), key=lambda x: x[1]):
            mat.dump(writer)
        summary = 'Exported with {numparts} parts'.format(
            numparts=len(part_dict)
        )
        Reporter.info(summary)


def sort_bones(arm):
    return [] if arm is None else arm.bones


def collect_bones(options):
    sorted_bones = sort_bones(options.arm.data)

    bones = [Bone(bone) for bone in sorted_bones]
    bone_parents = [sorted_bones.find(b.parent.name)
                            if b.parent is not None
                            else None for b in sorted_bones]
    return bones, bone_parents


def collect_constraints(bones, options):
    armature = options.arm
    pose_data = options.arm.pose
    bone_name_to_index = { b.bone.name: i for (i, b) in enumerate(bones) }
    constraint_list = []


    class CopyConstraint(object):
        ID_Constraint_Copy = b"COPY"

        def __init__(self, pbone, c):
            if c.euler_order != "AUTO":
                Reporter.user_error("copy constraints only supports Order: Default")
            if c.target != armature:
                Reporter.user_error("copy constraints support only targeting own armature")
            self.bone_idx = bone_name_to_index[pbone.name]
            self.target_idx = bone_name_to_index[c.subtarget]
            self.influence = c.influence
            self.use_x, self.use_y, self.use_z = c.use_x, c.use_y, c.use_z
            self.invert_x, self.invert_y, self.invert_z = c.invert_x, c.invert_y, c.invert_z
            self.mix_mode = ({
                "REPLACE": 0x0,
                "BEFORE": 0x1,
                "AFTER": 0x2,
            }).get(c.mix_mode, None)
            if self.mix_mode is None:
                Reporter.user_error("mix mode {mode} is not supported", mode=c.mix_mode)
            space_ids = {
                "LOCAL": 0x0,
                "LOCAL_WITH_PARENT": 0x1,
                "POSE": 0x2,
            }
            self.owner_space = space_ids.get(c.owner_space, None)
            if self.owner_space is None:
                Reporter.user_error("owner space 'global' is not supported, use 'pose' perhaps")
            self.target_space = space_ids.get(c.target_space, None)
            if self.target_space is None:
                Reporter.user_error("target space 'global' is not supported, use 'pose' perhaps")

        def dump(self, writer):
            writer.write_packed(">4B", *CopyConstraint.ID_Constraint_Copy)
            writer.write_packed(">2I", self.bone_idx, self.target_idx)
            writer.write_packed(">f", self.influence)
            option_bits = 0
            option_bits += 0x1 * self.use_x
            option_bits += 0x2 * self.use_y
            option_bits += 0x4 * self.use_y
            option_bits += 0x10 * self.invert_x
            option_bits += 0x20 * self.invert_y
            option_bits += 0x40 * self.invert_z
            writer.write_packed(">B", option_bits)
            writer.write_packed(">B", self.mix_mode)
            writer.write_packed(">B", self.owner_space)
            writer.write_packed(">B", self.target_space)

    def handle_copy_rot(pbone, constraint):
        constraint_list.append(CopyConstraint(pbone, constraint))

    def handle_inverse_kinematics(pbone, constraint):
        Reporter.warning("inverse kinematics constraint not yet supported")

    def handle_constraint(pbone, constraint):
        with Reporter.in_context("On bone {bname}, constraint {cname}", bname=pbone.name, cname=constraint.name):
            if constraint.mute:
                return
            if not constraint.is_valid:
                Reporter.warning("constraint is not valid and has been skipped")
                return
            if constraint.type == "COPY_ROTATION":
                handle_copy_rot(pbone, constraint)
            elif constraint.type == "IK":
                handle_inverse_kinematics(pbone, constraint)
            else:
                Reporter.user_error("unsupported constraint")

    for b in bones:
        pbone = pose_data.bones[b.bone.name]
        for con in pbone.constraints:
            handle_constraint(pbone, con)
    return constraint_list


class SkeletonV1(object):

    def __init__(self, options):
        self.bones, bone_parent_indices = collect_bones(options)
        self.bone_parent_indices = [
            (p if p is not None else 0xFF) for p in bone_parent_indices
        ]
        if len(self.bones) > 0xFF:
            Reporter.error("Too many bones")

    def dump(self, writer):
        numbones = len(self.bones)
        writer.write_packed(">I", 1)
        writer.write_packed(">B", numbones)
        for bone in self.bones:
            bone.dump(writer)
        writer.write_packed(">{nums}B".format(nums=numbones), *self.bone_parent_indices)
        message = 'Exported {numbones} bones'.format(numbones=numbones)
        Reporter.info(message)


class SkeletonV2(SkeletonV1):

    def __init__(self, options):
        super().__init__(options)
        self.bones, bone_parent_indices = collect_bones(options)
        self.bone_parent_indices = [
            (p if p is not None else 0xFFFFFFFF) for p in bone_parent_indices
        ]
        self.bone_constraints = collect_constraints(self.bones, options)
        if len(self.bones) > 0xFFFFFFFF:
            Reporter.error("Too many bones")
        if len(self.bone_constraints) > 0xFFFFFFFF:
            Reporter.error("Too many bone constraints")

    def dump(self, writer):
        numbones = len(self.bones)
        writer.write_packed(">I", 2)
        writer.write_packed(">I", numbones)
        for bone in self.bones:
            bone.dump(writer)
        writer.write_packed(">{nums}I".format(nums=numbones), *self.bone_parent_indices)
        numconstraints = len(self.bone_constraints)
        writer.write_packed(">I", numconstraints)
        for constraint in self.bone_constraints:
            constraint.dump(writer)
        message = 'Exported {numbones} bones, with {numconstraints} constraints'.format(
            numbones=numbones, numconstraints=numconstraints)
        Reporter.info(message)


class ActionV1(object):

    def __init__(self, options):
        armature = options.armature
        action = options.action
        offset = action.mcprops.offset
        bone_data_re = re.compile(R"pose\.bones\[\"(.*)\"\]\.(rotation_quaternion|location|scale)")
        bone_dict = defaultdict(lambda: ([None] * 3, [None] * 4, [None] * 3))
        armBones = sort_bones(armature)
        self.curve_count = 0
        for curve in action.fcurves:
            match = bone_data_re.match(curve.data_path)
            if match is None:
                continue
            name, key = match.groups()
            if name not in armBones:
                continue
            if key == 'location' and not armBones[name].use_connect:
                bone_dict[name][0][curve.array_index] = curve
                self.curve_count += 1
            elif key == 'rotation_quaternion':
                bone_dict[name][1][curve.array_index] = curve
                self.curve_count += 1
            elif key == 'scale':
                bone_dict[name][2][curve.array_index] = curve
                self.curve_count += 1
        bone_count = len(bone_dict)
        if bone_count > 255:
            Reporter.error("Too many bones to export")
        self.actions = [BoneAction(name, curves, offset)
                        for name, curves in bone_dict.items()]

    def dump(self, writer):
        bone_count = len(self.actions)
        writer.write_packed(">B", 1)
        writer.write_packed(">B", bone_count)
        for bone in self.actions:
            bone.dump(writer)
        summary = 'Exported {bones} bones with {curves} animated paths'.format(
            bones=bone_count,
            curves=self.curve_count
        )
        Reporter.info(summary)


MESH_VERSIONS = [
    ("V1", "Version 1", "Version 1 of MD model files. Rev1_010814"),
    ("V2", "Version 2", "Version 2 of MD model files. Rev1_300316"),
]
MESH_DEFAULT_VER = "V2"


known_mesh_exporters = {
    "V1": MeshV1,
    "V2": MeshV2,
    None: MeshV2,
}


class MeshExportOptions(object):
    obj = None
    arm = None
    uv_layer = None

    filepath = None
    uuid = None
    version = None


def export_mesh(context, options: MeshExportOptions):
    filepath = options.filepath
    # Write file header
    bitmask = 0xFFFFFFFF
    uuid_vec = options.uuid
    mesh = options.obj.data
    with ExitStack() as stack:
        if context.mode == 'EDIT_MESH':
            bmc = bmesh.from_edit_mesh(mesh)
            stack.callback(bmesh.update_edit_mesh, mesh)
            bm = bmc.copy()
            stack.callback(bm.free)
        else:
            bm = bmesh.new()
            stack.callback(bm.free)
            bm.from_mesh(mesh)
        # bmesh.ops.triangulate(bm, faces=bm.faces)
        try:
            MeshClass = known_mesh_exporters[options.version]
            model = MeshClass(options, bm)
        except (KeyError, NotImplementedError):
            Reporter.fatal("Version {v} is not implemented yet", v=options.version)
        # No more error (apart from IO) should happen now
        with Writer(filepath) as writer:
            writer.write_bytes(b'MHFC MDL')
            writer.write_packed(">4I",
                                uuid_vec[0] & bitmask,
                                uuid_vec[1] & bitmask,
                                uuid_vec[2] & bitmask,
                                uuid_vec[3] & bitmask)
            writer.write_string(mesh.mcprops.artist)
            model.dump(writer)


SKL_VERSIONS = [
    ("V1", "Version 1", "Version 1 of SKL model files. Rev1_010814"),
    ("V2", "Version 2", "Version 2 of SKL model files. Rev1_241220"),
]
SKL_DEFAULT_VER = "V2"


known_skel_exporters = {
    "V1": SkeletonV1,
    "V2": SkeletonV2,
    None: SkeletonV2,
}


class SkeletonExportOptions(object):
    arm = None

    filepath = None
    uuid = None
    version = None


def export_skl(context, options):
    # Write file header
    bitmask = 0xFFFFFFFF
    uuid_vec = options.uuid
    filepath = options.filepath
    arm = options.arm
    try:
        SkeletonClass = known_skel_exporters[options.version]
        skeleton = SkeletonClass(options)
    except (KeyError, NotImplementedError):
        Reporter.fatal("Version {v} is not implemented yet", v=options.version)
    with Writer(filepath) as writer:
        writer.write_bytes(b'MHFC SKL')
        writer.write_packed(">4I",
                            uuid_vec[0] & bitmask,
                            uuid_vec[1] & bitmask,
                            uuid_vec[2] & bitmask,
                            uuid_vec[3] & bitmask)
        writer.write_string(arm.data.mcprops.artist)
        skeleton.dump(writer)


class ActionExportOptions(object):
    action = None
    armature = None

    filepath = None
    version = None


def export_action(context, options):
    filepath = options.filepath
    action = options.action
    actions = ActionV1(options)
    with Writer(filepath) as writer:
        writer.write_bytes(b'MHFC ANM')
        writer.write_string(action.mcprops.artist)
        actions.dump(writer)
