#!/bin/bash

# Copyright (C) 2019 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Updates all dependenices listed in the pom
# placing them in the m2 subdirectory.
m2_dir=$(dirname "$0")/m2
git rm ${m2_dir}/*
mvn \
  -DoutputDirectory=${m2_dir} \
  -DincludeScope=runtime \
  -DexcludeArtifactIds=google-java-format,javax.inject,jsr250-api,checker-compat-qual \
  -DexcludeGroupIds=com.android.support \
  dependency:copy-dependencies

git add ${m2_dir}/*
