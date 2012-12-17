#version 150 core

uniform sampler2D texture;

in vec2 pass_Texture;

out vec4 out_Color;

void main(void) {
	out_Color = texture2D(texture, pass_Texture);
	if (out_Color.a < 0.5) discard;
}
