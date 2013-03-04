#version 150 core

in float pass_Distance;

out vec4 out_Color;

void main(void) {
	out_Color = vec4(1.0, 0.95, 0.8, 1.0 / (0.001 + pass_Distance * pass_Distance / 3.0));
}
