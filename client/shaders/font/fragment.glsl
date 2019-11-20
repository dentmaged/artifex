#version 330

in vec2 tc;

FS_OUT(colour)

tex font;

uniform vec4 colour;

const float width = 0.5;
const float edge = 0.1;

const float borderWidth = 0;
const float borderEdge = 0.4;

const vec2 offset = vec2(0);

const vec3 outlineColour = vec3(1, 0, 0);

void main(void) {
	float distance = 1.0 - texture2D(font, tc).a;
	float alpha = 1.0 - smoothstep(width, width + edge, distance);

	float distance2 = 1.0 - texture2D(font, tc + offset).a;
	float outlineAlpha = 1.0 - smoothstep(borderWidth, borderWidth + borderEdge, distance2);

	float overallAlpha = alpha + (1.0 - alpha) * outlineAlpha;
	vec3 overallColour = mix(outlineColour, colour.xyz, alpha / overallAlpha);

	out_colour = vec4(overallColour, overallAlpha * colour.w);
}
