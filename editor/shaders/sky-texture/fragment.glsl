#version 330

in vec3 pos;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(normal)
FS_OUT(other)
FS_OUT(albedo)

#include "functions.glsl"
#include "sky.glsl"

void main(void) {
	vec3 position = normalize(pos);
	Colour colour = getSkyColour(position);

	out_diffuse = colour.diffuse;
	out_normal = vec4(1);
	out_other = vec4(0, 0, colour.emissive, 0);
	out_albedo = colour.diffuse;
}
