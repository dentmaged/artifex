void emit(vec4 diffuse, vec3 normal, vec3 emissive, float metallic, float roughness, float ao) {
	out_diffuse = vec4(pow(diffuse.xyz, vec3(GAMMA)), diffuse.w);
	out_normal = vec4(normal, 0.5);
	out_other = vec4(ao, roughness, metallic, 1);
	out_bloom = vec4(emissive, 1);
	out_godrays = vec4(0, 0, 0, 1);
}
