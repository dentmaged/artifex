#version 330

in mat3 tbn;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

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
	float ao = usesAOMap ? texture2D(ao, tc).r : 1;

	vec3 mappedNormal = texture2D(normal, tc).xyz * 2 - 1;
	emit(vec4(mix(diffuse.xyz, colour.xyz, colour.w), 1), normalize(tbn * mappedNormal), texture2D(specular, tc).r * diffuse.xyz, metallic, roughness, ao);
}
