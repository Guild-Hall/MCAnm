package com.github.worldsender.mcanm.client.model;

import java.lang.reflect.Type;
import java.util.Objects;

import com.github.worldsender.mcanm.client.ClientLoader;
import com.github.worldsender.mcanm.client.mcanmmodel.ModelMCMD;
import com.github.worldsender.mcanm.common.CommonLoader;
import com.github.worldsender.mcanm.common.skeleton.ISkeleton;
import com.github.worldsender.mcanm.common.skeleton.LegacyModelAsSkeleton;
import com.github.worldsender.mcanm.common.skeleton.SkeletonMCSKL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Contains references to all required file to display the whole model. This includes the mesh-model ({@link ModelMCMD})
 * and the skeleton file ({@link SkeletonMCSKL})
 *
 * @author WorldSEnder
 */
@SuppressWarnings("deprecation") // We handle the legacy, not consume it
@OnlyIn(Dist.CLIENT)
public class ModelDescription {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ModelDescription.class, new DescriptionDeserializer())
            .create();

    private final ModelMCMD model;
    private final ISkeleton skeleton;

    private ModelDescription(
            ModelMCMD model,
            ISkeleton skeleton) {
        this.model = Objects.requireNonNull(model);
        this.skeleton = Objects.requireNonNull(skeleton);
    }

    public static ModelDescription parse(JsonObject modelContents) {
        return GSON.fromJson(modelContents, ModelDescription.class);
    }

    public ModelMCMD getModel() {
        return model;
    }

    public ISkeleton getSkeleton() {
        return skeleton;
    }

    private static class DescriptionDeserializer implements JsonDeserializer<ModelDescription> {
        private ISkeleton loadSkeleton(boolean legacy, JsonObject jsonObject) {
            if (!legacy && !jsonObject.has("skeleton")) {
                return ISkeleton.EMPTY;
            }
            ResourceLocation skeletonLocation = legacy
                    ? new ResourceLocation(jsonObject.get("mesh").getAsString())
                    : new ResourceLocation(jsonObject.get("skeleton").getAsString());

            return legacy ? loadLegacySkeleton(skeletonLocation) : CommonLoader.loadSkeleton(skeletonLocation);
        }

        private LegacyModelAsSkeleton loadLegacySkeleton(ResourceLocation skeletonLocation) {
            return CommonLoader.loadLegacySkeleton(skeletonLocation);
        }

        @Override
        public ModelDescription deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            int version = jsonObject.get("version").getAsInt();
            // Currently version is ignored
            if (version != 0 && version != 1) {
                throw new JsonParseException("Unsupported model version");
            }
            boolean isLegacy = version == 0;
            ISkeleton skeleton = loadSkeleton(isLegacy, jsonObject);

            ResourceLocation modelLocation = new ResourceLocation(jsonObject.get("mesh").getAsString());
            ModelMCMD mesh = ClientLoader.loadModel(modelLocation, skeleton);

            return new ModelDescription(mesh, skeleton);
        }
    }

}
