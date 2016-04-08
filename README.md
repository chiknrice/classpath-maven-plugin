<!--
  ~ Copyright (c) 2016 Ian Bondoc
  ~
  ~ This file is part of classpath-maven-plugin
  ~
  ~ classpath-maven-plugin is free software: you can redistribute it and/or modify it under the terms of the GNU General
  ~ Public License as published by the Free Software Foundation, either version 3 of the License, or(at your option) any
  ~ later version.
  ~
  ~ classpath-maven-plugin is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
  ~ implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  ~ details.
  ~
  ~ You should have received a copy of the GNU General Public License along with this program. If not, see
  ~ <http://www.gnu.org/licenses/>.
  ~
  -->

# classpath-maven-plugin [![Build Status](https://travis-ci.org/chiknrice/classpath-maven-plugin.svg?branch=master)](https://travis-ci.org/chiknrice/classpath-maven-plugin)
Maven plugin which uses dependency and jar plugins to create a jar containing a classpath file.  The value added to using
dependency plugin is the ability to include the project artifact in the classpath.