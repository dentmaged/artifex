#version 330

in vec3 s_normal;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex blendmap;
tex backgroundTexture;
tex rTexture;
tex gTexture;
tex bTexture;
uniform vec4 colour;

uniform mat4 viewMatrix;

#include "material.glsl"

void main(void) {
	vec4 blendMapColour = texture2D(blendmap, tc);

	float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
	vec2 tiled = tc * 270;
	vec4 backgroundTextureColour = texture2D(backgroundTexture, tiled) * backTextureAmount;
	vec4 rTextureColour = texture2D(rTexture, tiled) * blendMapColour.r;
	vec4 gTextureColour = texture2D(gTexture, tiled) * blendMapColour.g;
	vec4 bTextureColour = texture2D(bTexture, tiled) * blendMapColour.b;
	vec4 albedo = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;
	albedo.xyz = mix(albedo.xyz, colour.xyz, colour.w);

	emit(albedo, s_normal, 0, 0, 1, 1);
}
