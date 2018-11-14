#version 330

in vec2 position;
in mat4 projectionViewTransformationMatrix;
in vec4 textureOffsets;
in float blendFactor;

out vec2 tc;
out vec4 offsets;
out float blend;

uniform float rows;

void main(void) {
	gl_Position = projectionViewTransformationMatrix * vec4(position, 0, 1);

	tc = position * 0.5 + 0.5;
	tc.y = 1 - tc.y;
	tc /= rows;

	offsets = textureOffsets;
	blend = blendFactor;
}
