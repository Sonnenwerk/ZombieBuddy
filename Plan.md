1. Fix `tryMatchAndConvert` method in `zbUtils.java` to properly handle primitive unboxing for all numeric types.
   - Specifically handle when target parameter is a primitive wrapper (like `Integer.class`) vs primitive (`int.class`).
   - Use `Reflect.box(paramType).isAssignableFrom(arg.getClass())` or explicitly map primitive classes to their wrappers if arg matches.
   - For Kahlua numbers, ensure `Double` maps correctly to `double`, `Double`, `float`, `Float`, etc.
2. Complete pre commit steps
   - Request another code review to verify the fixes.
3. Submit change
   - Call `submit` to commit the change.
