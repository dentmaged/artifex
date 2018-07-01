#version 330

in vec3 pos;

FS_OUT(colour)

texCube environment;

const float pi = 3.141592653589793238;

void main(void) {
	vec3 N = normalize(pos);
	vec3 irradiance = vec3(0);

	vec3 right = cross(vec3(0, 1, 0), N);
	vec3 up = cross(N, right);

	float sampleDelta = 0.025;
	float sampleCount = 0;

	for (float phi = 0; phi < 2 * pi; phi += sampleDelta) {
		for (float theta = 0; theta < 0.5 * pi; theta += sampleDelta) {
			vec3 tangentSample = vec3(sin(theta) * cos(phi), sin(theta) * sin(phi), cos(theta)); // spherical coordinates
			vec3 direction = tangentSample.x * right + tangentSample.y * up + tangentSample.z * N;

			irradiance += texture(environment, direction).rgb * cos(theta) * sin(theta);
			sampleCount++;
		}
	}

	out_colour = vec4(pi * irradiance / float(sampleCount), 1);
}
