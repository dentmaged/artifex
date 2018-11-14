#version 330

FS_OUT(diffuse)
FS_OUT(albedo)

uniform vec3 colour;

void main(void) {
	out_diffuse = vec4(colour, 1);
	out_albedo = vec4(colour, 1);
}
