#version 330

in vec3 s_normal;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex albedo;
tex specular;
tex metallic;
tex roughness;
tex ao;

uniform vec4 colour;
uniform bool usesAOMap;
uniform bool blending;

#include "material.glsl"

void main(void) {
	vec4 diffuse = texture2D(albedo, tc);
	if (diffuse.w < 0.5)
		discard;

	float metallic = texture2D(metallic, tc).r;
	float roughness = texture2D(roughness, tc).r;
	float ao = usesAOMap ? texture2D(ao, tc).r : 1;

	emit(vec4(mix(diffuse.xyz, colour.xyz, colour.w), 1), s_normal, texture2D(specular, tc).r * diffuse.xyz, metallic, roughness, ao);
}
