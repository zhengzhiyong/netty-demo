package com.netty.chartgroup2.group;

import com.netty.chartgroup2.group.service.GroupSession;
import com.netty.chartgroup2.session.SessionFactory;
import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GroupSessionMemoryImpl implements GroupSession {
    private final Map<String, Group> groupMap = new ConcurrentHashMap<>();

    @Override
    public Group createGroup(String name, Set<String> members) {
        Group group = new Group(name, members);
        return groupMap.putIfAbsent(name, group);
    }

    @Override
    public Group joinMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().add(member);
            return value;
        });
    }

    @Override
    public Group removeMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().remove(member);
            return value;
        });
    }

    @Override
    public Group removeGroup(String name) {
        return groupMap.remove(name);
    }

    @Override
    public Set<String> getMembers(String name) {
        return groupMap.getOrDefault(name, Group.EMPTY_GROUP).getMembers();
    }

    @Override
    public List<Channel> getMembersChannel(String name) {
        //判断群聊存不存在
        if(groupMap.get(name) == null){
            return null;
        }
        return getMembers(name).stream()
                .map(member -> SessionFactory.getSession().getChannel(member))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUsers(String name) {
        if(groupMap.get(name) == null){
            return null;
        }
        return new ArrayList<>(groupMap.get(name).getMembers());
    }
}

