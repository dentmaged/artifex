#version 330

in vec2 blur[5];

FS_OUT(colour)

tex originalTexture;

void main(void) {
	out_colour = vec4(0.0);
	out_colour += texture2D(originalTexture, blur[0]);
	out_colour += texture2D(originalTexture, blur[1]);

	out_colour += texture2D(originalTexture, blur[2]);

	out_colour += texture2D(originalTexture, blur[3]);
	out_colour += texture2D(originalTexture, blur[4]);
	out_colour *= 0.2;
}
