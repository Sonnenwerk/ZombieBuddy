//go:build !windows

package main

import "fmt"

func testLoadDLL(pzPath string) error {
	fmt.Println("\nTesting DLL Load is only supported on Windows.")
	return nil
}
