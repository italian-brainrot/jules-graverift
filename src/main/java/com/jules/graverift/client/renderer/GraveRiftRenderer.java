package com.jules.graverift.client.renderer;

import com.jules.graverift.entity.GraveRiftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class GraveRiftRenderer extends EntityRenderer<GraveRiftEntity> {
    public GraveRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GraveRiftEntity entity) {
        return null;
    }

    @Override
    public void render(GraveRiftEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Center the render at the entity's position, elevated slightly
        poseStack.translate(0.0D, 1.25D, 0.0D);

        // Make the rift face the camera/player
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        // Gentle pulsing and swaying rotation effect
        float age = entity.tickCount + partialTicks;
        float scale = 1.0F + 0.08F * (float) Math.sin(age * 0.1F);
        poseStack.scale(scale * 1.2F, scale * 2.2F, scale * 1.2F);

        // Use EndPortal render type for an eerie, deep multi-layered parallax void effect
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.endPortal());

        Matrix4f matrix = poseStack.last().pose();

        // Outer diamond / tear quad
        renderDiamondQuad(consumer, matrix, 0x99, 0x11, 0xAA, 200);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    private void renderDiamondQuad(VertexConsumer consumer, Matrix4f matrix, int r, int g, int b, int a) {
        // Top vertex
        consumer.addVertex(matrix, 0.0F, 0.6F, 0.0F).setColor(r, g, b, a).setUv(0.5F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
        // Left vertex
        consumer.addVertex(matrix, -0.5F, 0.0F, 0.0F).setColor(r, g, b, a).setUv(0.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
        // Bottom vertex
        consumer.addVertex(matrix, 0.0F, -0.6F, 0.0F).setColor(r, g, b, a).setUv(0.5F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
        // Right vertex
        consumer.addVertex(matrix, 0.5F, 0.0F, 0.0F).setColor(r, g, b, a).setUv(1.0F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(15728880).setNormal(0.0F, 0.0F, 1.0F);
    }
}
