/*
This software includes code from Emerald Engine:

MIT License

Copyright (c) 2018 Lage Ragnarsson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
uniform vec3 sunDirection;
uniform vec3 sunColour;

uniform vec3 baseColour;
uniform vec3 topColour;

texCube skybox;
uniform bool proceduralSky;

struct Colour {
	vec4 diffuse;
	vec4 bloom;
	vec4 godrays;
};

Colour getSkyProcedural(vec3 direction) {
	Colour col;
	float r = rand(direction.xy) * 0.005;

	vec3 diffuse = mix(baseColour, topColour, max(dot(direction, vec3(0, 1, 0)), 0)) + vec3(r);

	float sunStrength = max(0, 100 * pow(max(dot(direction, normalize(sunDirection - 9 * direction / 10)), 0), 50));
	float sunPostProcess = sunStrength > 0.8 ? 1 : 0;
	vec3 sun = sunStrength * sunColour * sunColour;

	col.diffuse = vec4(pow(diffuse.xyz, vec3(GAMMA)) + sun, 1);
	col.bloom = vec4(sun, sunPostProcess * 0.25);
	col.godrays = vec4(sunColour, 1) * sunPostProcess;

	return col;
}

Colour getSkySample(vec3 direction) {
	Colour col;

	col.diffuse = vec4(pow(texture(skybox, direction).xyz, vec3(GAMMA)), 1);
	col.bloom = vec4(0, 0, 0, 1);
	col.godrays = vec4(0, 0, 0, 1);

	return col;
}

Colour getSkyColour(vec3 direction) {
	if (proceduralSky)
		return getSkyProcedural(direction);
	else
		return getSkySample(direction);
}
