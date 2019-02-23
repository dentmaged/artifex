#version 330

in vec2 tc;

FS_OUT(colour)

tex a;

const float radius = 0.75;
const float softness = 0.4;
const float intensity = 0.25;

void main(void) {
	out_colour = texture2D(a, tc);
	vec2 position = tc - vec2(0.5);

	float len = length(position);
	float vignette = smoothstep(radius, radius - softness, len);
	out_colour.xyz = pow(out_colour.xyz, vec3(1.0 / GAMMA));
	out_colour.xyz = mix(out_colour.xyz, out_colour.xyz * vignette, intensity);
}
