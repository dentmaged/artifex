#version 330

FS_OUT(colour)

tex exposure;
tex previousExposure;

uniform float exposureSpeed;

void main(void) {
	float exposure = 0.5 / max(dot(mix(texture2D(previousExposure, vec2(0.5)).xyz, texture2D(exposure, vec2(0.5)).xyz, exposureSpeed), vec3(0.2125, 0.7154, 0.0721)), 0.01);
	out_colour = vec4(exposure, exposure, exposure, 1);
}
