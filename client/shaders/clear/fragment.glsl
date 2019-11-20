#version 330

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

uniform vec3 background;

void main(void) {
	out_diffuse = vec4(background, 1.0);
	out_other = vec4(0.0, 0.0, 0.0, 1.0);
	out_normal = vec4(0.0, 0.0, 0.0, 1.0);
	out_albedo = vec4(background, 1.0);
}
