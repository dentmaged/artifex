#version 330

in vec3 viewPosition;
in vec3 s_normal;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

tex albedo;
tex specular;
tex metallic;
tex roughness;
tex ao;
tex depthMap;

uniform mat4 inverseProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform vec4 colour;
uniform bool usesAOMap;
uniform bool blending;

#include "util.glsl"
#include "lighting.glsl"

void main(void) {
	vec4 diffuse = texture2D(albedo, tc);
	if (diffuse.w == 0)
		discard;

	float metallic = texture2D(metallic, tc).r;
	float roughness = texture2D(roughness, tc).r;
	float ao = usesAOMap ? texture2D(ao, tc).r : 1;
	float specular = texture2D(specular, tc).r;

	if (specular > 0)
		out_diffuse = diffuse;
	else
		out_diffuse = vec4(performLighting(viewPosition, s_normal, diffuse.xyz, metallic, 0.5, roughness, ao), diffuse.w);

	out_other = vec4(0);
	out_normal = vec4(0);
	out_bloom = vec4(specular * diffuse.xyz + out_diffuse.xyz - vec3(1.1), out_diffuse.w);
	out_godrays = vec4(0);
}
