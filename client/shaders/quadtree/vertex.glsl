#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;

out vec4 viewPosition;
out vec3 s_normal;
out vec2 tc;
out vec2 coords;
out float discardFragment;

tex heightmap;

uniform int morph;
uniform float size;
uniform vec3 location;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;
uniform mat4 normalMatrix;

const float maxColour = 256 * 256 * 256  * 0.5f;
const float maxHeight = 16 / maxColour;
const float morphRegion = 0.3;

const int MORPH_TOP = 1;
const int MORPH_LEFT = 2;
const int MORPH_BOTTOM = 4;
const int MORPH_RIGHT = 8;

float getHeight(vec2 coords) {
	vec4 height = texture2D(heightmap, coords, 0);
	float worldHeight = (int(height.w * 255) << 24) | (int(height.x * 255) << 16) | (int(height.y * 255) << 8) | (int(height.z * 255) << 0);
	worldHeight += maxColour;

	// return worldHeight * maxHeight;
	return height.r * 256;
}

vec3 getNormal(vec2 coords) {
	float change = 1.0 / 256.0;
	float heightL = getHeight(coords + vec2(-change, 0));
	float heightR = getHeight(coords + vec2(change, 0));

	float heightD = getHeight(coords + vec2(0, -change));
	float heightU = getHeight(coords + vec2(0, change));

	return normalize(vec3(heightL - heightR, 2, heightD - heightU));
}

bool edgePresent(int edge) {
	int e = morph / edge;

	return 2 * (e / 2) != e;
}

float getMorphFactor() {
	float morphFactor = 0;
	if (edgePresent(MORPH_TOP) && position.y >= 1 - morphRegion)
		morphFactor = max(1 - clamp((1 - position.y) / morphRegion, 0, 1), morphFactor);

	if (edgePresent(MORPH_LEFT) && position.x <= morphRegion)
		morphFactor = max(1 - clamp(position.x / morphRegion, 0, 1), morphFactor);

	if (edgePresent(MORPH_BOTTOM) && position.y <= morphRegion)
		morphFactor = max(1 - clamp(position.y / morphRegion, 0, 1), morphFactor);

	if (edgePresent(MORPH_RIGHT) && position.x >= 1 - morphRegion)
		morphFactor = max(1 - clamp((1 - position.x) / morphRegion, 0, 1), morphFactor);

	return morphFactor;
}

void main(void) {
	vec4 worldPosition = transformationMatrix * vec4(position, 1);

	float grid = size / 32.0;
	coords = (worldPosition.xz - location.xy) / location.z;
	worldPosition.xz = floor(worldPosition.xz / grid) * grid;

	float morphFactor = getMorphFactor();
	if (morphFactor > 0) {
		grid *= 2;
		vec2 morphedWorldPosition = floor(worldPosition.xz / grid) * grid;

		worldPosition.xz = mix(worldPosition.xz, morphedWorldPosition, morphFactor);
	}

	vec2 heightmapCoords = (worldPosition.xz - location.xy) / location.z;
	worldPosition.y = getHeight(heightmapCoords);
	viewPosition = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * viewPosition;

	discardFragment = 0;
	if (coords.x > 1 || coords.y > 1 || coords.x < 0 || coords.y < 0)
		discardFragment = 1;

	s_normal = normalize((normalMatrix * vec4(getNormal(coords), 0)).xyz);
	tc = textureCoordinates;
}
