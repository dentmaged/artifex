#version 330

FS_OUT(diffuse)
FS_OUT(bloom)
FS_OUT(godrays)

uniform vec3 colour;

void main(void) {
	out_diffuse = vec4(colour, 1);
	out_bloom = vec4(0, 0, 0, 1);
	out_godrays = vec4(0, 0, 0, 1);
}
