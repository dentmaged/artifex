#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;

void main(void) {
	out_colour = texture2D(scene, tc);
	out_colour = out_colour * (out_colour.r * 0.2126) + (out_colour.g * 0.7152) + (out_colour.b * 0.0722);
}
