#version 150 core

uniform mat4 projectionMatrix;
uniform mat4 modelviewMatrix;

in vec4 in_Position;
in vec4 in_Color;

out vec4 pass_Color;

void main(void) {
	gl_Position = projectionMatrix * modelviewMatrix * in_Position;	
	pass_Color = in_Color;
}
