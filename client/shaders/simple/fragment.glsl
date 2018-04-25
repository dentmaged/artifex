#version 330

in vec2 tc;

FS_OUT(colour)

tex a;

void main(void) {
	out_colour = texture2D(a, tc);
}
