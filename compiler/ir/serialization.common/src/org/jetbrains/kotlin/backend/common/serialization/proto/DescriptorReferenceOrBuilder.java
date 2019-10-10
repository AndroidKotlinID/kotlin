// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

public interface DescriptorReferenceOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.jetbrains.kotlin.backend.common.serialization.proto.DescriptorReference)
    org.jetbrains.kotlin.protobuf.MessageLiteOrBuilder {

  /**
   * <code>repeated int32 package_fq_name = 1;</code>
   */
  java.util.List<java.lang.Integer> getPackageFqNameList();
  /**
   * <code>repeated int32 package_fq_name = 1;</code>
   */
  int getPackageFqNameCount();
  /**
   * <code>repeated int32 package_fq_name = 1;</code>
   */
  int getPackageFqName(int index);

  /**
   * <code>repeated int32 class_fq_name = 2;</code>
   */
  java.util.List<java.lang.Integer> getClassFqNameList();
  /**
   * <code>repeated int32 class_fq_name = 2;</code>
   */
  int getClassFqNameCount();
  /**
   * <code>repeated int32 class_fq_name = 2;</code>
   */
  int getClassFqName(int index);

  /**
   * <code>required int32 name = 3;</code>
   */
  boolean hasName();
  /**
   * <code>required int32 name = 3;</code>
   */
  int getName();

  /**
   * <code>required int32 flags = 4;</code>
   */
  boolean hasFlags();
  /**
   * <code>required int32 flags = 4;</code>
   */
  int getFlags();

  /**
   * <code>optional int64 uniq_id_index = 5;</code>
   */
  boolean hasUniqIdIndex();
  /**
   * <code>optional int64 uniq_id_index = 5;</code>
   */
  long getUniqIdIndex();
}