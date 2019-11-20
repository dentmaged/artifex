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

tex skybox;
uniform bool proceduralSky;
uniform float tickTime;

uniform vec3 fogColour;
uniform float blendFogTransitionStart;
uniform float blendFogTransitionEnd;
uniform float blendFogMultiplier;

struct Colour {
	vec4 diffuse;
	float emissive;
};

Colour getSkyProcedural(vec3 direction) {
	Colour col;
	float r = rand(direction.xy) * 0.005;
	float time = tickTime * 0.002 + 1.8879;

	vec3 diffuse = mix(baseColour, topColour * 0.3, max(dot(direction, vec3(0, 1, 0)), 0.0)) + vec3(r);

	float sunStrength = max(0, 100 * pow(max(dot(direction, normalize(sunDirection - 9 * direction / 10)), 0), 50));
	float sunPostProcess = sunStrength > 0.8 ? 1 : 0;
	vec3 sun = sunStrength * sunColour * sunColour;

	vec3 colour = diffuse + sun;
	if (tc.y > blendFogTransitionStart)
		colour = mix(fogColour, colour, clamp((blendFogTransitionEnd - tc.y) * blendFogMultiplier, 0.0, 1.0));

	col.diffuse = vec4(pow(colour, vec3(GAMMA)), 1);
	col.emissive = sunPostProcess;

	return col;
}

Colour getSkySample(vec3 direction) {
	Colour col;

	vec3 colour = texture2D(skybox, tc).xyz;
	if (tc.y > blendFogTransitionStart)
		colour = mix(fogColour, colour, clamp((blendFogTransitionEnd - tc.y) * blendFogMultiplier, 0.0, 1.0));
	col.diffuse = vec4(pow(colour, vec3(GAMMA)), 1.0);
	// col.diffuse.xyz *= 3;
	col.emissive = 0;

	return col;
}

Colour getSkyColour(vec3 direction) {
	if (proceduralSky)
		return getSkyProcedural(direction);
	else
		return getSkySample(direction);
}
