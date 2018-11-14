#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex bloom;

void main(void) {
	out_colour = texture2D(scene, tc) * texture2D(bloom, tc).z;
	out_colour = out_colour * (out_colour.x * 0.2126) + (out_colour.y * 0.7152) + (out_colour.z * 0.0722);
}
