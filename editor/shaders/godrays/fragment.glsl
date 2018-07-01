#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex godrays;
tex exposure;

uniform vec2 lightPositionOnScreen;
uniform int numSamples;

const float decay = 1;
const float density = 1;
const float weight = 0.01;

void main() {
	vec2 deltaTextCoord = vec2(tc - lightPositionOnScreen.xy);
	vec2 textCoord = tc.xy;
	deltaTextCoord *= (1 / float(numSamples)) * density;
	vec3 colour = vec3(0);
	float illuminationDecay = 1;

	for (int i = 0; i < numSamples; i++) {
		textCoord -= deltaTextCoord;
		vec3 sample = texture2D(godrays, textCoord).xyz;
		sample *= illuminationDecay * weight;

		colour += sample;
		illuminationDecay *= decay;
	}

	vec2 dc = smoothstep(0.215, 0.615, abs(vec2(0.5) - lightPositionOnScreen));
	float screenEdgeFactor = clamp(1 - (dc.x + dc.y), 0, 1);
	colour.xyz *= screenEdgeFactor;

	out_colour = texture2D(scene, tc);
	out_colour.xyz = pow(out_colour.xyz, vec3(1.0 / GAMMA));
	out_colour.xyz += colour * texture2D(exposure, vec2(0.5)).x;
}
