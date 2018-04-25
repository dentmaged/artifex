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
#version 330

in vec4 worldPosition;
in vec3 s_normal;
in vec3 pos;
in vec2 tc;

FS_OUT(diffuse)
FS_OUT(position)
FS_OUT(bloom)
FS_OUT(godrays)

uniform vec3 sunDirection;
uniform vec3 sunColour;

uniform vec3 baseColour;
uniform vec3 topColour;

tex modelTexture;

float rand(vec2 co){
	return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main(void) {
	vec3 dir = normalize(pos);
	float r = rand(pos.xy) * 0.005;

	out_diffuse = vec4(mix(baseColour, topColour, max(dot(dir, vec3(0, 1, 0)), 0)), 1) + vec4(r, r, r, 1);
	out_position = worldPosition;

	out_bloom = vec4(0);
	out_godrays = vec4(0);

	float sun = max(0, 100 * pow(max(dot(dir, normalize(sunDirection - 9 * dir / 10)), 0), 50));
	vec3 sunC = sun * sunColour * sunColour;

	out_diffuse.xyz += sunC;
	out_bloom.xyz += sunC;

	out_diffuse = vec4(pow(out_diffuse.xyz, vec3(GAMMA)), 1);
	out_bloom.a = 1;

	if (sun > 0.8)
		sun = 1;
	else
		sun = 0;

	out_godrays = vec4(sunColour, 1) * sun;
}
