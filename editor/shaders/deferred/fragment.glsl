#version 330

in vec2 tc;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex diffuse;
tex other;
tex normal;
tex depthMap;

uniform mat4 projectionMatrix;
uniform mat4 inverseProjectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 inverseViewMatrix;

#include "functions.glsl"
#include "util.glsl"
#include "sky.glsl"
#include "lighting.glsl"

void main(void) {
	vec4 diffuse = texture2D(diffuse, tc);
	vec4 other = texture2D(other, tc);
	vec4 normal = texture2D(normal, tc);
	other.x = max(other.x, 0.001);

	float metallic = other.y;
	float specular = normal.w;
	float roughness = other.x;
	float emissive = other.z;

	if (emissive > 0)
		out_diffuse = vec4(diffuse.xyz, 1);
	else
		out_diffuse = vec4(performLighting(getPosition(tc), normal.xyz, diffuse.xyz, metallic, 0.5, roughness), 1);

	out_other = vec4(other);
	out_normal = vec4(normal);
	out_albedo = vec4(0); // writing is disabled
}
