#version 150 core

uniform mat4 projectionMatrix;
uniform mat4 modelviewMatrix;

in vec4 in_Position;

out float pass_Distance;

void main(void) {
	gl_Position = projectionMatrix * modelviewMatrix * in_Position;
	pass_Distance = length(in_Position);
}
