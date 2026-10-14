//go:build windows

package main

import (
	"fmt"
	"os"
	"path/filepath"
	"syscall"
)

func testLoadDLL(pzPath string) error {
	fmt.Println("\nTesting DLL Load...")

	dllPath := filepath.Join(pzPath, "zbNative.dll")
	if _, err := os.Stat(dllPath); os.IsNotExist(err) {
		return fmt.Errorf("zbNative.dll not found in %s", pzPath)
	}

	jreBin := filepath.Join(pzPath, "jre64", "bin")
	jreServer := filepath.Join(jreBin, "server")

	// Temporarily add JRE to PATH so dependencies can be resolved
	oldPath := os.Getenv("PATH")
	newPath := jreServer + string(os.PathListSeparator) + jreBin + string(os.PathListSeparator) + oldPath
	os.Setenv("PATH", newPath)
	defer os.Setenv("PATH", oldPath)

	// LoadLibrary
	dll, err := syscall.LoadLibrary(dllPath)
	if err != nil {
		fmt.Printf("[!] Error: Failed to load %s. It might be blocked by antivirus or missing dependencies. Details: %v\n", dllPath, err)
		return err
	}
	defer syscall.FreeLibrary(dll)

	fmt.Printf("[OK] Successfully loaded zbNative.dll. Dependencies and antivirus exceptions seem correct.\n")
	return nil
}
