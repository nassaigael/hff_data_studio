package com.henri_fraise.hff_data_studio.mapper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final UserCategoryMapper userCategoryMapper;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
}
