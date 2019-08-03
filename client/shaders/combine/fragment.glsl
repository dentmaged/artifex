#version 330

in vec2 tc;

FS_OUT(colour)

tex ldr;
tex bloomOne;
tex bloomTwo;
tex bloomThree;
tex exposure;

vec3 A = vec3(0.15);
vec3 B = vec3(0.50);
vec3 C = vec3(0.10);
vec3 D = vec3(0.20);
vec3 E = vec3(0.02);
vec3 F = vec3(0.30);
vec3 W = vec3(11.2);

vec3 Uncharted2Tonemap(vec3 x) {
   return ((x * (A * x + C * B) + D * E) / (x * (A * x + B) + D * F)) - E / F;
}

void main(void) {
	vec3 hdrColour = texture2D(ldr, tc).xyz;
	vec3 bloomColour = texture2D(bloomOne, tc).xyz + texture2D(bloomTwo, tc).xyz + texture2D(bloomThree, tc).xyz;
	hdrColour += bloomColour;

	float exposure = texture2D(exposure, vec2(0.5)).x;
	vec3 curr = Uncharted2Tonemap(2 * exposure * hdrColour);
	vec3 whiteScale = vec3(1.0) / Uncharted2Tonemap(W);
	out_colour = vec4(curr * whiteScale, 1.0);
}
