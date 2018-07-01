#version 330

in vec3 position;
in vec2 textureCoordinates;

out mat4 viewModelMatrix;
out vec4 worldPosition;
out vec4 viewPosition;
out vec4 clipSpace;
out vec4 shadowCoords;
out vec2 tc;
out float visibility;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;
uniform mat4 toShadowMapSpace;
uniform vec2 tiling;

uniform float density;
uniform float gradient;
uniform float shadowDistance;

const float transitionDistance = 3;

void main(void) {
	worldPosition = transformationMatrix * vec4(position, 1);
	shadowCoords = toShadowMapSpace * worldPosition;
	viewPosition = viewMatrix * worldPosition;
	clipSpace = projectionMatrix * viewPosition;
	gl_Position = clipSpace;

	tc = textureCoordinates;
	tc *= tiling;

	float distance = length(viewPosition.xyz);
	visibility = exp(-pow((distance * density), gradient));
	visibility = clamp(visibility, 0, 1);

	distance = distance - (shadowDistance - transitionDistance);
	distance = distance / transitionDistance;
	shadowCoords.w = clamp(1 - distance, 0, 1);
}
