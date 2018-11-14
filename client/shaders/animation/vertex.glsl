#version 330

in vec3 position;
in vec2 textureCoordinates;
in vec3 normal;
in ivec3 jointIndices;
in vec3 weights;

out vec3 s_normal;
out vec2 tc;

uniform mat4 projectionViewTransformationMatrix;
uniform mat4 normalMatrix;

uniform mat4 jointTransforms[MAX_JOINTS];

uniform float numberOfRows;
uniform vec2 offset;

void main(void) {
	vec4 totalPosition = vec4(0);
	vec4 totalNormal = vec4(0);

	for (int i = 0; i < MAX_WEIGHTS; i++) {
		mat4 jointTransform = jointTransforms[jointIndices[i]];
		vec4 posePosition = jointTransform * vec4(position, 1);
		totalPosition += posePosition * weights[i];

		vec4 worldNormal = mat3(jointTransform) * normal;
		totalNormal += worldNormal * weights[i];
	}

	gl_Position = projectionViewTransformationMatrix * vec4(totalPosition, 1);

	s_normal = normalize(mat3(normalMatrix) * totalNormal);
	tc = (textureCoordinates / numberOfRows) + offset;
}
