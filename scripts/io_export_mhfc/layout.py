import bpy

from bpy.types import Panel, Menu, UIList, Header, UILayout

from .operators import ObjectExporter, AnimationExporter, SkeletonExporter,\
    TechneImport, SceneExporter, TabulaImport


class LayoutWrapper(object):
    _layout = None

    def __init__(self, layout):
        self._layout = layout

    def __dir__(self):
        return dir(self._layout)

    def __getattr__(self, name):
        if name in {"row", "column", "column_flow", "box", "split"}:
            fn = getattr(self._layout, name)

            def wrapped(*args, **wargs):
                sublayout = fn(*args, **wargs)
                return LayoutWrapper(sublayout)
            return wrapped
        elif name in {"prop", "props_enum", "prop_menu_enum", "prop_enum",
                      "prop_search", "template_ID", "template_ID_preview",
                      "template_any_ID", "template_path_builder",
                      "template_curve_mapping", "template_color_ramp",
                      "template_icon_view", "template_histogram",
                      "template_waveform", "template_vectorscope",
                      "template_layers", "template_color_picker",
                      "template_image", "template_movieclip",
                      "template_track", "template_marker",
                      "template_movieclip_information", "template_component_menu",
                      "template_colorspace_settings",
                      "template_colormanaged_view_settings"}:
            def wrapped(data, prop, *args, **wargs):
                wrapper = self

                class BoundArgs(object):
                    _tests = []

                    def add_test(self, predicate, error_message):
                        self._tests.append((predicate, error_message))
                        return self

                    def display(self):
                        row_layout = wrapper._layout.row()
                        original_call = getattr(row_layout, name)(data, prop, *args, **wargs)
                        errored = False
                        for pre, mess in self._tests:
                            if not pre(getattr(data, prop)):
                                wrapper._layout.label(text=mess, icon="ERROR")
                                errored = True
                        if errored:
                            row_layout.label(icon="ERROR")
                        return original_call

                return BoundArgs()
            return wrapped
        elif callable(getattr(self._layout, name)):
            def wrapped(*args, **wargs):
                return getattr(self._layout, name)(*args, **wargs)
            return wrapped
        return getattr(self._layout, name)


class AnimationExportMenu(Menu):
    bl_label = "Export Action"
    bl_idname = "MCANM_MT_ExportAction"

    def draw(self, context):
        layout = self.layout
        sce = context.scene
        if context.space_data.mode == 'ACTION':
            row = layout.row()
            op = row.operator(AnimationExporter.bl_idname)
            # op.armature = context.object
            op.offset = sce.frame_start
            if not context.space_data.action:
                row.enabled = False
            else:
                op.arm_action = context.space_data.action.name


class AnimationExportHeader(Header):
    bl_label = "MCAnimation Export Menu"
    bl_idname = "MCANM_HT_ExportAction"
    bl_space_type = "DOPESHEET_EDITOR"

    def draw(self, context):
        layout = self.layout
        layout.menu(AnimationExportMenu.bl_idname)


class MeshDataPanel(Panel):
    # MC Panel under Object
    bl_idname = "MCANM_PT_MeshData"
    bl_label = "Minecraft Animated"
    bl_region_type = "WINDOW"
    bl_space_type = "PROPERTIES"
    bl_context = "data"

    @classmethod
    def poll(cls, context):
        return context.object is not None and context.object.type == 'MESH'

    def draw(self, context):
        layout = LayoutWrapper(self.layout)
        data = context.object.data
        props = context.object.data.mcprops

        layout.prop(props, 'artist', text="Artist name").display()
        layout.prop(props, 'armature', text="Armature", icon="ARMATURE_DATA")\
            .display()
        layout.prop_search(props, 'uv_layer', data, 'uv_layers', text="UV Layer", icon="GROUP_UVS")\
            .add_test(lambda uv: uv, "Select a UV map.")\
            .add_test(lambda uv: uv in data.uv_layers, "Invalid UV map")\
            .display()


class SceneDataPanel(Panel):
    # MC Panel under Object
    bl_idname = "MCANM_PT_SceneData"
    bl_label = "Minecraft Animated"
    bl_region_type = "WINDOW"
    bl_space_type = "PROPERTIES"
    bl_context = "scene"

    def draw(self, context):
        layout = LayoutWrapper(self.layout)
        sceprops = context.scene.mcprops
        layout.prop(sceprops, 'projectname').display()
        layout.prop_search(sceprops, 'object', bpy.data, 'objects').display()
        layout.prop_search(sceprops, 'skeleton', bpy.data, 'objects').display()
        layout.operator(SceneExporter.bl_idname)


def export_func(self, context):
    self.layout.operator(
        ObjectExporter.bl_idname, text="Minecraft Animated models (.mcmd)")
    self.layout.operator(
        SkeletonExporter.bl_idname, text="Minecraft Animated skeletons (.mcskl)")
    self.layout.operator(
        AnimationExporter.bl_idname, text="Minecraft Animations (.mcanm)")
    self.layout.operator(
        SceneExporter.bl_idname, text="Minecraft Animated (.mc*)")


def import_func(self, context):
    self.layout.operator(
        TechneImport.bl_idname, text="Techne Models (.tcn)")
    self.layout.operator(
        TabulaImport.bl_idname, text="Tabula Models (.tbl)")

classes = [
    AnimationExportMenu,
    AnimationExportHeader,
    MeshDataPanel,
    SceneDataPanel
]
register_classes, unregister_classes = bpy.utils.register_classes_factory(classes)

def register():
    bpy.types.TOPBAR_MT_file_export.append(export_func)
    bpy.types.TOPBAR_MT_file_import.append(import_func)
    register_classes()

def unregister():
    unregister_classes()
    bpy.types.TOPBAR_MT_file_export.remove(export_func)
    bpy.types.TOPBAR_MT_file_import.remove(import_func)

