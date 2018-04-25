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
	out_colour.rgb = mix(out_colour.rgb, out_colour.rgb * vignette, intensity);
}
