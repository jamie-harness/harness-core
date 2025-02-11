// Copyright 2022 Harness Inc. All rights reserved.
// Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
// that can be found in the licenses directory at the root of this repository, also available at
// https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.

// Code generated by MockGen. DO NOT EDIT.
// Source: command.go

// Package exec is a generated GoMock package.
package exec

import (
	io "io"
	reflect "reflect"

	gomock "github.com/golang/mock/gomock"
)

// MockCommandFactory is a mock of CommandFactory interface.
type MockCommandFactory struct {
	ctrl     *gomock.Controller
	recorder *MockCommandFactoryMockRecorder
}

// MockCommandFactoryMockRecorder is the mock recorder for MockCommandFactory.
type MockCommandFactoryMockRecorder struct {
	mock *MockCommandFactory
}

// NewMockCommandFactory creates a new mock instance.
func NewMockCommandFactory(ctrl *gomock.Controller) *MockCommandFactory {
	mock := &MockCommandFactory{ctrl: ctrl}
	mock.recorder = &MockCommandFactoryMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockCommandFactory) EXPECT() *MockCommandFactoryMockRecorder {
	return m.recorder
}

// Command mocks base method.
func (m *MockCommandFactory) Command(name string, args ...string) Command {
	m.ctrl.T.Helper()
	varargs := []interface{}{name}
	for _, a := range args {
		varargs = append(varargs, a)
	}
	ret := m.ctrl.Call(m, "Command", varargs...)
	ret0, _ := ret[0].(Command)
	return ret0
}

// Command indicates an expected call of Command.
func (mr *MockCommandFactoryMockRecorder) Command(name interface{}, args ...interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	varargs := append([]interface{}{name}, args...)
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Command", reflect.TypeOf((*MockCommandFactory)(nil).Command), varargs...)
}

// MockCommand is a mock of Command interface.
type MockCommand struct {
	ctrl     *gomock.Controller
	recorder *MockCommandMockRecorder
}

// MockCommandMockRecorder is the mock recorder for MockCommand.
type MockCommandMockRecorder struct {
	mock *MockCommand
}

// NewMockCommand creates a new mock instance.
func NewMockCommand(ctrl *gomock.Controller) *MockCommand {
	mock := &MockCommand{ctrl: ctrl}
	mock.recorder = &MockCommandMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockCommand) EXPECT() *MockCommandMockRecorder {
	return m.recorder
}

// CombinedOutput mocks base method.
func (m *MockCommand) CombinedOutput() ([]byte, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "CombinedOutput")
	ret0, _ := ret[0].([]byte)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// CombinedOutput indicates an expected call of CombinedOutput.
func (mr *MockCommandMockRecorder) CombinedOutput() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "CombinedOutput", reflect.TypeOf((*MockCommand)(nil).CombinedOutput))
}

// Output mocks base method.
func (m *MockCommand) Output() ([]byte, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Output")
	ret0, _ := ret[0].([]byte)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// Output indicates an expected call of Output.
func (mr *MockCommandMockRecorder) Output() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Output", reflect.TypeOf((*MockCommand)(nil).Output))
}

// Pid mocks base method.
func (m *MockCommand) Pid() int {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Pid")
	ret0, _ := ret[0].(int)
	return ret0
}

// Pid indicates an expected call of Pid.
func (mr *MockCommandMockRecorder) Pid() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Pid", reflect.TypeOf((*MockCommand)(nil).Pid))
}

// ProcessState mocks base method.
func (m *MockCommand) ProcessState() ProcessState {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "ProcessState")
	ret0, _ := ret[0].(ProcessState)
	return ret0
}

// ProcessState indicates an expected call of ProcessState.
func (mr *MockCommandMockRecorder) ProcessState() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "ProcessState", reflect.TypeOf((*MockCommand)(nil).ProcessState))
}

// Run mocks base method.
func (m *MockCommand) Run() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Run")
	ret0, _ := ret[0].(error)
	return ret0
}

// Run indicates an expected call of Run.
func (mr *MockCommandMockRecorder) Run() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Run", reflect.TypeOf((*MockCommand)(nil).Run))
}

// Start mocks base method.
func (m *MockCommand) Start() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Start")
	ret0, _ := ret[0].(error)
	return ret0
}

// Start indicates an expected call of Start.
func (mr *MockCommandMockRecorder) Start() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Start", reflect.TypeOf((*MockCommand)(nil).Start))
}

// StderrPipe mocks base method.
func (m *MockCommand) StderrPipe() (io.ReadCloser, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "StderrPipe")
	ret0, _ := ret[0].(io.ReadCloser)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// StderrPipe indicates an expected call of StderrPipe.
func (mr *MockCommandMockRecorder) StderrPipe() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "StderrPipe", reflect.TypeOf((*MockCommand)(nil).StderrPipe))
}

// StdinPipe mocks base method.
func (m *MockCommand) StdinPipe() (io.WriteCloser, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "StdinPipe")
	ret0, _ := ret[0].(io.WriteCloser)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// StdinPipe indicates an expected call of StdinPipe.
func (mr *MockCommandMockRecorder) StdinPipe() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "StdinPipe", reflect.TypeOf((*MockCommand)(nil).StdinPipe))
}

// StdoutPipe mocks base method.
func (m *MockCommand) StdoutPipe() (io.ReadCloser, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "StdoutPipe")
	ret0, _ := ret[0].(io.ReadCloser)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// StdoutPipe indicates an expected call of StdoutPipe.
func (mr *MockCommandMockRecorder) StdoutPipe() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "StdoutPipe", reflect.TypeOf((*MockCommand)(nil).StdoutPipe))
}

// String mocks base method.
func (m *MockCommand) String() string {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "String")
	ret0, _ := ret[0].(string)
	return ret0
}

// String indicates an expected call of String.
func (mr *MockCommandMockRecorder) String() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "String", reflect.TypeOf((*MockCommand)(nil).String))
}

// Wait mocks base method.
func (m *MockCommand) Wait() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Wait")
	ret0, _ := ret[0].(error)
	return ret0
}

// Wait indicates an expected call of Wait.
func (mr *MockCommandMockRecorder) Wait() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Wait", reflect.TypeOf((*MockCommand)(nil).Wait))
}

// WithCombinedOutput mocks base method.
func (m *MockCommand) WithCombinedOutput(writer io.Writer) Command {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "WithCombinedOutput", writer)
	ret0, _ := ret[0].(Command)
	return ret0
}

// WithCombinedOutput indicates an expected call of WithCombinedOutput.
func (mr *MockCommandMockRecorder) WithCombinedOutput(writer interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "WithCombinedOutput", reflect.TypeOf((*MockCommand)(nil).WithCombinedOutput), writer)
}

// WithDir mocks base method.
func (m *MockCommand) WithDir(arg0 string) Command {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "WithDir", arg0)
	ret0, _ := ret[0].(Command)
	return ret0
}

// WithDir indicates an expected call of WithDir.
func (mr *MockCommandMockRecorder) WithDir(arg0 interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "WithDir", reflect.TypeOf((*MockCommand)(nil).WithDir), arg0)
}

// WithEnvVars mocks base method.
func (m *MockCommand) WithEnvVars(kvps ...string) Command {
	m.ctrl.T.Helper()
	varargs := []interface{}{}
	for _, a := range kvps {
		varargs = append(varargs, a)
	}
	ret := m.ctrl.Call(m, "WithEnvVars", varargs...)
	ret0, _ := ret[0].(Command)
	return ret0
}

// WithEnvVars indicates an expected call of WithEnvVars.
func (mr *MockCommandMockRecorder) WithEnvVars(kvps ...interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "WithEnvVars", reflect.TypeOf((*MockCommand)(nil).WithEnvVars), kvps...)
}

// WithEnvVarsMap mocks base method.
func (m *MockCommand) WithEnvVarsMap(kvps map[string]string) Command {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "WithEnvVarsMap", kvps)
	ret0, _ := ret[0].(Command)
	return ret0
}

// WithEnvVarsMap indicates an expected call of WithEnvVarsMap.
func (mr *MockCommandMockRecorder) WithEnvVarsMap(kvps interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "WithEnvVarsMap", reflect.TypeOf((*MockCommand)(nil).WithEnvVarsMap), kvps)
}

// WithStderr mocks base method.
func (m *MockCommand) WithStderr(writer io.Writer) Command {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "WithStderr", writer)
	ret0, _ := ret[0].(Command)
	return ret0
}

// WithStderr indicates an expected call of WithStderr.
func (mr *MockCommandMockRecorder) WithStderr(writer interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "WithStderr", reflect.TypeOf((*MockCommand)(nil).WithStderr), writer)
}

// WithStdout mocks base method.
func (m *MockCommand) WithStdout(writer io.Writer) Command {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "WithStdout", writer)
	ret0, _ := ret[0].(Command)
	return ret0
}

// WithStdout indicates an expected call of WithStdout.
func (mr *MockCommandMockRecorder) WithStdout(writer interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "WithStdout", reflect.TypeOf((*MockCommand)(nil).WithStdout), writer)
}
