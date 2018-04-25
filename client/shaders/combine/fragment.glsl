#version 330

in vec2 tc;

FS_OUT(colour)

tex a;
tex b;

uniform float exposure;

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
	vec3 hdrColour = texture(a, tc).rgb;
	vec3 bloomColour = texture(b, tc).rgb;
	hdrColour += bloomColour;

	vec3 curr = Uncharted2Tonemap(2 * exposure * hdrColour);
	vec3 whiteScale = vec3(1.0) / Uncharted2Tonemap(W);
	vec3 color = curr * whiteScale;
	out_colour = vec4(color, 1.0);
}
