#version 330

in vec2 blur[11];

FS_OUT(colour)

tex originalTexture;

void main(void) {
	out_colour = vec4(0.0);
	out_colour += texture2D(originalTexture, blur[0]) * 0.0093;
	out_colour += texture2D(originalTexture, blur[1]) * 0.028002;

	out_colour += texture2D(originalTexture, blur[2]) * 0.065984;
	out_colour += texture2D(originalTexture, blur[3]) * 0.121703;

	out_colour += texture2D(originalTexture, blur[4]) * 0.175713;
	out_colour += texture2D(originalTexture, blur[5]) * 0.198596;

	out_colour += texture2D(originalTexture, blur[6]) * 0.175713;
	out_colour += texture2D(originalTexture, blur[7]) * 0.121703;

	out_colour += texture2D(originalTexture, blur[8]) * 0.065984;
	out_colour += texture2D(originalTexture, blur[9]) * 0.028002;
	out_colour += texture2D(originalTexture, blur[10]) * 0.0093;
}
