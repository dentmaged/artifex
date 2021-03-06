#version 330

in vec3 s_tangent;
in vec3 s_bitangent;
in vec3 s_normal;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex albedo;
tex normal;
tex specular;
tex metallic;
tex roughness;
tex ao;

uniform vec4 colour;
uniform bool usesAOMap;

#include "material.glsl"

void main(void) {
	vec4 diffuse = texture2D(albedo, tc);
	if (diffuse.a < 0.5)
		discard;

	float metallic = texture2D(metallic, tc).r;
	float roughness = texture2D(roughness, tc).r;
	float ao = usesAOMap ? texture2D(ao, tc).r : 1.0;

	mat3 tbn = mat3(s_tangent, s_bitangent, s_normal);

	vec3 mappedNormal = normalize(texture2D(normal, tc).xyz * 2.0 - 1.0);
	emit(vec4(mix(diffuse.xyz, colour.xyz, colour.w), 1.0), normalize(tbn * mappedNormal), texture2D(specular, tc).r, metallic, roughness, ao);
}
