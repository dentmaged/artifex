void emit(vec4 diffuse, vec3 normal, float emissive, float metallic, float roughness, float ao) {
#ifdef LIGHTING_GLSL
	out_diffuse = vec4(performLighting(viewPosition, normal, diffuse.xyz, metallic, 0.5, roughness), diffuse.w);
#else
	out_diffuse = vec4(pow(diffuse.xyz, vec3(GAMMA)), diffuse.w);
#endif
	out_normal = vec4(normal, ao);
	out_other = vec4(roughness, metallic, emissive, 0);
	out_albedo = vec4(pow(diffuse.xyz, vec3(GAMMA)), diffuse.w);
}

void emitDecal(vec4 diffuse, float emissive, float metallic, float roughness, float ao) {
	out_diffuse = vec4(pow(diffuse.xyz, vec3(GAMMA)), diffuse.w);
	out_normal = vec4(0, 0, 0, ao); // glColorMask disables writing to RGB
	out_other = vec4(roughness, metallic, emissive, 0);
	out_albedo = vec4(pow(diffuse.xyz, vec3(GAMMA)), diffuse.w);
}
