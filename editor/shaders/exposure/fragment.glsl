#version 330

FS_OUT(colour)

tex exposure;
tex previousExposure;

uniform float exposureSpeed;
uniform float mip;

void main(void) {
	float exposure = 0.5 / max(dot(pow(mix(texture2D(previousExposure, vec2(0.5)).xyz, texture2D(exposure, vec2(0.5), mip).xyz, exposureSpeed), vec3(1.0 / GAMMA)), vec3(0.2125, 0.7154, 0.0721)), 0.01);
	out_colour = vec4(exposure, exposure, exposure, 1);
}
