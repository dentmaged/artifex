#version 330

in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(bloom)
FS_OUT(godrays)

tex diffuse;
tex other;
tex normal;
tex extra;
tex ssao;
tex bloom;
tex godrays;
tex depthMap;
tex scene;

uniform mat4 projectionMatrix;
uniform mat4 inverseProjectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 inverseViewMatrix;

#include "functions.glsl"
#include "util.glsl"
#include "sky.glsl"
#include "reflection.glsl"
#include "lighting.glsl"

void main(void) {
	vec4 diffuse = texture2D(diffuse, tc);
	vec4 other = texture2D(other, tc);
	vec4 normal = texture2D(normal, tc);
	vec4 bloom = texture2D(bloom, tc);

	float metallic = other.b;
	float specular = normal.a;
	float roughness = other.g;
	float ao = texture2D(ssao, tc).r + other.r;

	vec3 viewPosition = getPosition(tc);
	vec3 worldPosition = (inverseViewMatrix * vec4(viewPosition, 1)).xyz;

	if (dot(bloom.xyz, bloom.xyz) > 0)
		out_diffuse = vec4(diffuse.xyz, 1);
	else
		out_diffuse = vec4(performLighting(viewPosition, worldPosition, normal.xyz, diffuse.xyz, metallic, specular, roughness, ao), 1);

	out_other = vec4(0);
	out_normal = vec4(0);
	out_bloom = vec4(bloom.xyz + out_diffuse.xyz - vec3(1.1), bloom.w);
	out_godrays = texture2D(godrays, tc);
}
