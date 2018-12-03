#version 330

in vec3 vPosition;

FS_OUT(diffuse)
FS_OUT(albedo)

uniform vec3 colour;
uniform float plane;

uniform bool modo;

const float thickness = 0.025;
const float threshold = 0.5 - thickness;

const float circleCenter = 0.75;
const float circleThreshold = (thickness * 3) * (thickness * 3);

void main(void) {
	vec2 circleDist = vPosition.xy - vec2(circleCenter);
	float lengthSquared = dot(circleDist, circleDist);

	out_diffuse = vec4(colour, (!modo && (vPosition.x > threshold || vPosition.y > threshold)) || (modo && (lengthSquared > circleThreshold)) ? 1.0 : plane);
	out_albedo = out_diffuse;
}
