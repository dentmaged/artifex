#version 330

in vec2 tc;

FS_OUT(colour)

tex scene;
tex bloom;
tex exposure;

void main(void) {
	out_colour = texture2D(scene, tc);
	out_colour.xyz *= texture2D(bloom, tc).z + length(max((out_colour.xyz - vec3(2.1 - texture2D(exposure, vec2(0.5)).x)) / out_colour.xyz, 0));
}
