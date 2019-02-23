#version 330

in vec2 tc;
in vec4 offsets;
in float blend;

FS_OUT(diffuse)
FS_OUT(other)
FS_OUT(normal)
FS_OUT(albedo)

tex albedo;

void main(void) {
	out_diffuse = mix(texture2D(albedo, tc + offsets.xy), texture2D(albedo, tc + offsets.zw), blend);
	out_diffuse.xyz = pow(out_diffuse.xyz, vec3(GAMMA));

	out_other = vec4(0);
	out_normal = vec4(0);
	out_albedo = out_diffuse;
}
