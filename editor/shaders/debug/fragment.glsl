#version 330

FS_OUT(diffuse)
FS_OUT(albedo)

uniform vec3 colour;
uniform float alpha;

void main(void) {
	out_diffuse = vec4(colour, alpha);
	out_albedo = vec4(colour, 1);
}
